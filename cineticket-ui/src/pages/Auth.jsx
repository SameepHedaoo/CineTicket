import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../api/api";

function Auth() {
    const [mode, setMode] = useState("login");
    const [email, setEmail] = useState("user@example.com");
    const [password, setPassword] = useState("Password@123");
    const [message, setMessage] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get("redirect") || "/shows";

    const handleSubmit = (event) => {
        event.preventDefault();
        setLoading(true);
        setError(null);
        setMessage(null);

        const payload = { email, password };
        const request = mode === "login"
            ? api.post("/auth/login", payload)
            : api.post("/auth/register", payload);

        request
            .then((res) => {
                if (mode === "login") {
                    const token = res.data?.token;
                    if (!token) {
                        throw new Error("Token not found in response");
                    }
                    localStorage.setItem("token", token);
                    window.dispatchEvent(new Event("auth-changed"));
                    navigate(redirectTo);
                } else {
                    setMessage(res.data?.message || "Registration successful. Please login.");
                    setMode("login");
                }
            })
            .catch((err) => {
                console.error("Auth failed:", err);
                setError("Authentication failed");
            })
            .finally(() => setLoading(false));
    };

    return (
        <div className="page auth-page">
            <div className="page-header">
                <div>
                    <h2>{mode === "login" ? "Login" : "Register"}</h2>
                    <p className="subtle">
                        {mode === "login"
                            ? "Use your account to book seats."
                            : "Create an account to start booking."}
                    </p>
                </div>
                <div className="filters">
                    <button
                        className={mode === "login" ? "primary" : "secondary"}
                        onClick={() => setMode("login")}
                        type="button"
                    >
                        Login
                    </button>
                    <button
                        className={mode === "register" ? "primary" : "secondary"}
                        onClick={() => setMode("register")}
                        type="button"
                    >
                        Register
                    </button>
                </div>
            </div>

            <form className="card auth-card" onSubmit={handleSubmit}>
                <label className="field">
                    <span>Email</span>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </label>
                <label className="field">
                    <span>Password</span>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </label>
                <button className="primary" type="submit" disabled={loading}>
                    {loading ? "Please wait..." : mode === "login" ? "Login" : "Register"}
                </button>
            </form>

            {error && <div className="error">{error}</div>}
            {message && <div className="muted">{message}</div>}
        </div>
    );
}

export default Auth;
