import { NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const NAV_ITEMS = [
  { to: "/workflows", label: "Workflows" },
  { to: "/runs", label: "Runs" },
  { to: "/dlq", label: "Dead-letter queue" },
];

export function Sidebar() {
  const { username, logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="dot" />
        <span className="label">OrbitFlow</span>
      </div>

      <div className="sidebar-section-label">Monitor</div>
      <nav className="sidebar-nav">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `sidebar-link${isActive ? " active" : ""}`}
          >
            <span className="label">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <div className="sidebar-user-avatar" />
          <span className="sidebar-user-name label">{username}</span>
        </div>
        <button className="sidebar-logout" onClick={logout} title="Log out">
          Log out
        </button>
      </div>
    </aside>
  );
}
