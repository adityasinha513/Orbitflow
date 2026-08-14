import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { ProtectedLayout } from "./components/ProtectedLayout";
import { LoginPage } from "./pages/LoginPage";
import { WorkflowsPage } from "./pages/WorkflowsPage";
import { RunsPage } from "./pages/RunsPage";
import { RunDetailPage } from "./pages/RunDetailPage";
import { DlqPage } from "./pages/DlqPage";

export function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedLayout />}>
          <Route path="/workflows" element={<WorkflowsPage />} />
          <Route path="/runs" element={<RunsPage />} />
          <Route path="/runs/:runId" element={<RunDetailPage />} />
          <Route path="/dlq" element={<DlqPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/workflows" replace />} />
      </Routes>
    </AuthProvider>
  );
}
