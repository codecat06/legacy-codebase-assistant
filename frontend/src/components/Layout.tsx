import { NavLink, Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <div className="app-shell">
      <nav className="navbar">
        <div className="brand">
          <span className="brand-dot" />
          CodeIntel
        </div>
        <div className="nav-links">
          <NavLink to="/" end>
            Repo
          </NavLink>
          <NavLink to="/chat">Sohbet</NavLink>
          <NavLink to="/impact">Impact</NavLink>
        </div>
      </nav>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}