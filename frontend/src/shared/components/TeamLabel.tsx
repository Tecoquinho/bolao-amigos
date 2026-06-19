import { useMemo, useState } from "react";
import { getFlagImageUrl, getFriendlyTeamName } from "../utils/flags";

interface TeamLabelProps {
  name: string;
  fifaCode?: string | null;
  flagUrl?: string | null;
  flagPlacement?: "start" | "end";
  justify?: "start" | "end" | "center";
}

export function TeamLabel({ name, fifaCode, flagUrl, flagPlacement = "start", justify = "start" }: TeamLabelProps) {
  const [hasImageError, setHasImageError] = useState(false);
  const imageUrl = useMemo(() => getFlagImageUrl(flagUrl, fifaCode, name), [fifaCode, flagUrl, name]);
  const displayName = useMemo(() => getFriendlyTeamName(name), [name]);
  const shouldRenderImage = Boolean(imageUrl) && !hasImageError;
  const flagImage = shouldRenderImage ? (
    <img
      src={imageUrl ?? undefined}
      alt=""
      className="h-5 w-7 shrink-0 rounded-[4px] border border-black/10 object-cover"
      loading="lazy"
      onError={() => setHasImageError(true)}
    />
  ) : null;
  const justifyClassName =
    justify === "end"
      ? "justify-end"
      : justify === "center"
        ? "justify-center"
        : "justify-start";

  return (
    <span className={`inline-flex min-w-0 max-w-full flex-nowrap items-center gap-2 overflow-hidden whitespace-nowrap align-middle ${justifyClassName}`}>
      {flagPlacement === "start" ? flagImage : null}
      <span className="truncate whitespace-nowrap">{displayName}</span>
      {flagPlacement === "end" ? flagImage : null}
    </span>
  );
}
