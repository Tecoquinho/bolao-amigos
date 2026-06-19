const fifaCodeToCountryCode: Record<string, string> = {
  ALG: "dz",
  ARG: "ar",
  AUS: "au",
  AUT: "at",
  BEL: "be",
  BIH: "ba",
  BRA: "br",
  CAN: "ca",
  CIV: "ci",
  COD: "cd",
  COL: "co",
  CPV: "cv",
  CRO: "hr",
  CUW: "cw",
  CZE: "cz",
  ENG: "gb-eng",
  ECU: "ec",
  EGY: "eg",
  ESP: "es",
  FRA: "fr",
  GER: "de",
  GHA: "gh",
  HTI: "ht",
  HAI: "ht",
  IRN: "ir",
  IRQ: "iq",
  JOR: "jo",
  JPN: "jp",
  KOR: "kr",
  KSA: "sa",
  MAR: "ma",
  MEX: "mx",
  NED: "nl",
  NOR: "no",
  NZL: "nz",
  PAN: "pa",
  PAR: "py",
  POR: "pt",
  QAT: "qa",
  RSA: "za",
  SCO: "sco",
  SEN: "sn",
  SUI: "ch",
  SWE: "se",
  TUN: "tn",
  TUR: "tr",
  URU: "uy",
  USA: "us",
  UZB: "uz",
};

const explicitFlagUrlByFifaCode: Record<string, string> = {
  ENG: "https://upload.wikimedia.org/wikipedia/en/b/be/Flag_of_England.svg",
  SCO: "https://upload.wikimedia.org/wikipedia/commons/1/10/Flag_of_Scotland.svg",
};

const teamNameToFifaCode: Record<string, string> = {
  "Africa do Sul": "RSA",
  "Alemanha": "GER",
  "Argentina": "ARG",
  "Arabia Saudita": "KSA",
  "Argelia": "ALG",
  "Australia": "AUS",
  "Austria": "AUT",
  "Belgica": "BEL",
  "Bosnia e Herzegovina": "BIH",
  "Brasil": "BRA",
  "Cabo Verde": "CPV",
  "Canada": "CAN",
  "Catar": "QAT",
  "Colombia": "COL",
  "Coreia do Sul": "KOR",
  "Costa do Marfim": "CIV",
  "Croacia": "CRO",
  "Curacao": "CUW",
  "Egito": "EGY",
  "Equador": "ECU",
  "Escocia": "SCO",
  "Espanha": "ESP",
  "Estados Unidos": "USA",
  "Franca": "FRA",
  "Gana": "GHA",
  "Haiti": "HTI",
  "Inglaterra": "ENG",
  "Ira": "IRN",
  "Iraque": "IRQ",
  "Japao": "JPN",
  "Jordania": "JOR",
  "Marrocos": "MAR",
  "Mexico": "MEX",
  "Noruega": "NOR",
  "Nova Zelandia": "NZL",
  "Paises Baixos": "NED",
  "Panama": "PAN",
  "Paraguai": "PAR",
  "Portugal": "POR",
  "Republica Democratica do Congo": "COD",
  "Republica Tcheca": "CZE",
  "Senegal": "SEN",
  "Suecia": "SWE",
  "Suica": "SUI",
  "Tunisia": "TUN",
  "Turquia": "TUR",
  "Uruguai": "URU",
  "Uzbequistao": "UZB",
};

function normalizeTeamName(name: string) {
  return name
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .trim();
}

const friendlyTeamNameMap: Record<string, string> = {
  "Africa do Sul": "África Sul",
  "Arabia Saudita": "Arábia Saud.",
  "Bosnia e Herzegovina": "BiH",
  "Coreia do Sul": "Coreia Sul",
  "Estados Unidos": "Estados Unid.",
  "Nova Zelandia": "N. Zelândia",
  "Republica Democratica do Congo": "RD Congo",
  "Republica Tcheca": "Tchéquia",
};

function resolveFifaCode(fifaCode: string | null | undefined, teamName: string | null | undefined) {
  if (fifaCode) {
    return fifaCode.toUpperCase();
  }

  if (!teamName) {
    return null;
  }

  return teamNameToFifaCode[normalizeTeamName(teamName)] ?? null;
}

export function getFlagImageUrl(
  flagUrl: string | null | undefined,
  fifaCode: string | null | undefined,
  teamName: string | null | undefined,
) {
  if (flagUrl) {
    return flagUrl;
  }

  const resolvedFifaCode = resolveFifaCode(fifaCode, teamName);
  if (!resolvedFifaCode) {
    return null;
  }

  const normalizedCode = resolvedFifaCode.toUpperCase();
  if (explicitFlagUrlByFifaCode[normalizedCode]) {
    return explicitFlagUrlByFifaCode[normalizedCode];
  }

  const countryCode = fifaCodeToCountryCode[normalizedCode];
  if (!countryCode) {
    return null;
  }

  return `https://flagcdn.com/w40/${countryCode}.png`;
}

export function getFriendlyTeamName(name: string) {
  return friendlyTeamNameMap[normalizeTeamName(name)] ?? name;
}
