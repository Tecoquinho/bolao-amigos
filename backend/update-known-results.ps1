param(
    [string]$BaseUrl = "http://localhost:8080"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$confirmedResults = @(
    @{ MatchNumber = 1; HomeScore = 2; AwayScore = 0; Game = "Mexico x South Africa" },
    @{ MatchNumber = 2; HomeScore = 2; AwayScore = 1; Game = "South Korea x Czechia" },
    @{ MatchNumber = 3; HomeScore = 1; AwayScore = 1; Game = "Czechia x South Africa" },
    @{ MatchNumber = 4; HomeScore = 1; AwayScore = 0; Game = "Mexico x South Korea" },
    @{ MatchNumber = 7; HomeScore = 1; AwayScore = 1; Game = "Canada x Bosnia and Herzegovina" },
    @{ MatchNumber = 8; HomeScore = 1; AwayScore = 1; Game = "Qatar x Switzerland" },
    @{ MatchNumber = 9; HomeScore = 4; AwayScore = 1; Game = "Switzerland x Bosnia and Herzegovina" },
    @{ MatchNumber = 10; HomeScore = 6; AwayScore = 0; Game = "Canada x Qatar" },
    @{ MatchNumber = 13; HomeScore = 1; AwayScore = 1; Game = "Brazil x Morocco" },
    @{ MatchNumber = 14; HomeScore = 0; AwayScore = 1; Game = "Haiti x Scotland" },
    @{ MatchNumber = 19; HomeScore = 4; AwayScore = 1; Game = "United States x Paraguay" },
    @{ MatchNumber = 20; HomeScore = 2; AwayScore = 0; Game = "Australia x Turkiye" },
    @{ MatchNumber = 25; HomeScore = 7; AwayScore = 1; Game = "Germany x Curacao" },
    @{ MatchNumber = 26; HomeScore = 1; AwayScore = 0; Game = "Ivory Coast x Ecuador" },
    @{ MatchNumber = 31; HomeScore = 2; AwayScore = 2; Game = "Netherlands x Japan" },
    @{ MatchNumber = 32; HomeScore = 5; AwayScore = 1; Game = "Sweden x Tunisia" },
    @{ MatchNumber = 37; HomeScore = 1; AwayScore = 1; Game = "Belgium x Egypt" },
    @{ MatchNumber = 38; HomeScore = 2; AwayScore = 2; Game = "Iran x New Zealand" },
    @{ MatchNumber = 43; HomeScore = 0; AwayScore = 0; Game = "Spain x Cape Verde" },
    @{ MatchNumber = 44; HomeScore = 1; AwayScore = 1; Game = "Saudi Arabia x Uruguay" },
    @{ MatchNumber = 49; HomeScore = 3; AwayScore = 1; Game = "France x Senegal" },
    @{ MatchNumber = 50; HomeScore = 1; AwayScore = 4; Game = "Iraq x Norway" },
    @{ MatchNumber = 55; HomeScore = 3; AwayScore = 0; Game = "Argentina x Algeria" },
    @{ MatchNumber = 56; HomeScore = 3; AwayScore = 1; Game = "Austria x Jordan" },
    @{ MatchNumber = 61; HomeScore = 1; AwayScore = 1; Game = "Portugal x DR Congo" },
    @{ MatchNumber = 62; HomeScore = 1; AwayScore = 3; Game = "Uzbekistan x Colombia" },
    @{ MatchNumber = 67; HomeScore = 4; AwayScore = 2; Game = "England x Croatia" },
    @{ MatchNumber = 68; HomeScore = 1; AwayScore = 0; Game = "Ghana x Panama" }
)

Write-Host "Fetching matches from $BaseUrl ..."
$matches = [array](Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/matches")

if ($matches.Count -eq 0) {
    throw "No matches returned by API."
}

$matchIndex = @{}
foreach ($match in $matches) {
    $matchIndex[[int]$match.matchNumber] = $match
}

$updatedResults = New-Object System.Collections.Generic.List[object]

foreach ($result in $confirmedResults) {
    if (-not $matchIndex.ContainsKey([int]$result.MatchNumber)) {
        throw "Match not found for matchNumber $($result.MatchNumber)."
    }

    $match = $matchIndex[[int]$result.MatchNumber]
    $payload = @{
        homeScore = [int]$result.HomeScore
        awayScore = [int]$result.AwayScore
    } | ConvertTo-Json

    $response = Invoke-RestMethod `
        -Method Patch `
        -Uri "$BaseUrl/api/matches/$($match.id)/result" `
        -ContentType "application/json" `
        -Body $payload

    $updatedResults.Add([pscustomobject]@{
        MatchNumber = [int]$response.matchNumber
        Game = $result.Game
        Score = "$($response.homeScore) x $($response.awayScore)"
        Status = $response.status
    })
}

$finishedMatches = [array](Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/matches?status=FINISHED")
$ranking = [array](Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/ranking")
$topTen = @($ranking | Select-Object -First 10)

Write-Host ""
Write-Host "Updated matches: $($updatedResults.Count)"
$updatedResults | Sort-Object MatchNumber | Format-Table -AutoSize

Write-Host ""
Write-Host "Finished matches reported by API: $($finishedMatches.Count)"

Write-Host ""
Write-Host "Top 10 ranking:"
$topTen | Select-Object position, participantName, totalPoints, exactScoreHits, resultHits | Format-Table -AutoSize
