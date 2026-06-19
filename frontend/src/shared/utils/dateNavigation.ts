export interface DatedItem {
  startsAt: string;
  matchNumber?: number;
}

export interface DateGroup<T> {
  key: string;
  label: string;
  items: T[];
}

const DATE_KEY_FORMATTER = new Intl.DateTimeFormat("en-CA", {
  timeZone: "America/Sao_Paulo",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

const DISPLAY_DATE_FORMATTER = new Intl.DateTimeFormat("pt-BR", {
  timeZone: "America/Sao_Paulo",
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

export function getDateKey(value: string | Date) {
  return DATE_KEY_FORMATTER.format(value instanceof Date ? value : new Date(value));
}

export function formatDateLabel(dateKey: string) {
  const [year, month, day] = dateKey.split("-");
  const localDate = new Date(`${year}-${month}-${day}T12:00:00-03:00`);
  const todayKey = getDateKey(new Date());

  if (dateKey === todayKey) {
    return "Hoje";
  }

  return DISPLAY_DATE_FORMATTER.format(localDate);
}

export function groupItemsByDate<T extends DatedItem>(items: T[]): DateGroup<T>[] {
  const groups = new Map<string, T[]>();

  for (const item of items) {
    const key = getDateKey(item.startsAt);
    const currentItems = groups.get(key) ?? [];
    currentItems.push(item);
    groups.set(key, currentItems);
  }

  return Array.from(groups.entries())
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([key, groupedItems]) => ({
      key,
      label: formatDateLabel(key),
      items: groupedItems.sort((left, right) => {
        const startsAtComparison = left.startsAt.localeCompare(right.startsAt);
        if (startsAtComparison !== 0) {
          return startsAtComparison;
        }

        return (left.matchNumber ?? Number.MAX_SAFE_INTEGER) - (right.matchNumber ?? Number.MAX_SAFE_INTEGER);
      }),
    }));
}

export function resolveInitialDateIndex(dateKeys: string[]) {
  if (dateKeys.length === 0) {
    return -1;
  }

  const todayKey = getDateKey(new Date());
  const todayIndex = dateKeys.findIndex((dateKey) => dateKey === todayKey);

  if (todayIndex >= 0) {
    return todayIndex;
  }

  const nextIndex = dateKeys.findIndex((dateKey) => dateKey > todayKey);
  if (nextIndex >= 0) {
    return nextIndex;
  }

  return dateKeys.length - 1;
}
