param(
    [string]$BaseUrl = "http://localhost:8080"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "Fetching finished matches from $BaseUrl ..."
$finishedMatches = [array](Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/matches?status=FINISHED")

if ($finishedMatches.Count -eq 0) {
    throw "No finished matches returned by API."
}

$recalculatedMatches = New-Object System.Collections.Generic.List[object]

foreach ($match in ($finishedMatches | Sort-Object {[int]$_.matchNumber})) {
    if ($null -eq $match.homeScore -or $null -eq $match.awayScore) {
        throw "Finished match $($match.matchNumber) is missing official score."
    }

    $payload = @{
        homeScore = [int]$match.homeScore
        awayScore = [int]$match.awayScore
    } | ConvertTo-Json

    $response = Invoke-RestMethod `
        -Method Patch `
        -Uri "$BaseUrl/api/matches/$($match.id)/result" `
        -ContentType "application/json" `
        -Body $payload

    $recalculatedMatches.Add([pscustomobject]@{
        MatchNumber = [int]$response.matchNumber
        Game = "$($response.homeTeamName) x $($response.awayTeamName)"
        Score = "$($response.homeScore) x $($response.awayScore)"
        Status = $response.status
    })
}

$ranking = [array](Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/ranking")
$topTen = @($ranking | Select-Object -First 10)

Write-Host ""
Write-Host "Recalculated finished matches: $($recalculatedMatches.Count)"
$recalculatedMatches | Format-Table -AutoSize

Write-Host ""
Write-Host "Top 10 ranking:"
$topTen | Select-Object position, participantName, totalPoints, exactScoreHits, resultHits | Format-Table -AutoSize
