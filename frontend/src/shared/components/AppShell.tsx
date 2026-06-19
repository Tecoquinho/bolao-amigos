import { NavLink } from "react-router-dom";
import type { PropsWithChildren } from "react";

const navigation = [
  { to: "/", label: "Ranking" },
  { to: "/matches", label: "Jogos" },
];

function navClassName(isActive: boolean) {
  return [
    "flex w-full flex-col items-center justify-center rounded-xl border-[2.5px] border-[#1a3a2f] px-4 py-2.5 text-xs font-black transition",
    isActive
      ? "bg-[#205347] text-[#c8e8dc] shadow-[2px_3px_0_#1a3a2f]"
      : "bg-[#f0ede4] text-[#7a9e8e]",
  ].join(" ");
}

export function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="mx-auto min-h-screen max-w-[380px] bg-[#f0ede4] text-ink">
      <div className="min-h-screen pb-24">
        <main className="px-0 py-3">{children}</main>
      </div>
      <nav className="fixed bottom-0 left-0 right-0 mx-auto max-w-[380px] border-t-[2.5px] border-[#1a3a2f] bg-white px-3 pb-3.5 pt-2">
        <div className="grid grid-cols-2 gap-2">
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
