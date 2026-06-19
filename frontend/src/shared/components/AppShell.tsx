import { NavLink } from "react-router-dom";
import type { PropsWithChildren } from "react";

const navigation = [
  { to: "/", label: "Ranking" },
  { to: "/matches", label: "Jogos" },
];

function navClassName(isActive: boolean) {
  return [
    "flex w-full flex-col items-center justify-center rounded-3xl px-4 py-4 text-sm font-semibold transition",
    isActive
      ? "bg-pine text-white shadow-[0_10px_24px_rgba(32,83,71,0.25)]"
      : "bg-[#f3f0e7] text-ink/55 ring-1 ring-black/5 hover:bg-mist hover:text-pine",
  ].join(" ");
}

export function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="mx-auto min-h-screen max-w-md bg-[#f7f2e8] text-ink shadow-[0_0_0_1px_rgba(16,35,34,0.04)] sm:border-x sm:border-black/5">
      <div className="min-h-screen pb-24">
        <main className="px-4 py-5">{children}</main>
      </div>
      <nav className="fixed bottom-0 left-0 right-0 mx-auto max-w-md border-t border-black/5 bg-white/92 px-4 pb-5 pt-3 backdrop-blur">
        <div className="grid grid-cols-2 gap-3">
          {navigation.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === "/"}>
              {({ isActive }) => <span className={navClassName(isActive)}>{item.label}</span>}
            </NavLink>
          ))}
        </div>
      </nav>
    </div>
  );
}
