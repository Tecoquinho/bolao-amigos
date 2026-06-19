import { createBrowserRouter } from "react-router-dom";
import { App } from "./App";
import { RankingPage } from "../features/ranking/pages/RankingPage";
import { ParticipantDetailPage } from "../features/participants/pages/ParticipantDetailPage";
import { MatchesPage } from "../features/matches/pages/MatchesPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, element: <RankingPage /> },
      { path: "ranking", element: <RankingPage /> },
      { path: "participants/:participantId", element: <ParticipantDetailPage /> },
      { path: "matches", element: <MatchesPage /> },
    ],
  },
]);
