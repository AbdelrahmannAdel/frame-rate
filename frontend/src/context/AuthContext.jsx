import { createContext, useContext, useState, useEffect } from "react";
import { getUserIdFromToken, getUser, saveToken, clearToken } from "../api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(undefined);

    useEffect(() => {
        const userId = getUserIdFromToken();
        if (!userId) {
            setCurrentUser(null);
            return;
        }


        getUser(userId)
            .then((user) => setCurrentUser(user))
            .catch(() => {
                clearToken();
                setCurrentUser(null);
            });
    }, []);

    useEffect(() => {
        function handleExpired() {
            setCurrentUser(null);
        }

        window.addEventListener("auth:expired", handleExpired);
        return () => window.removeEventListener("auth:expired", handleExpired);
    }, []);

    function loginWithToken(token, user) {
        saveToken(token);
        setCurrentUser(user);
    }

    function logout() {
        clearToken();
        setCurrentUser(null);
    }

    return (
        <AuthContext.Provider value={{ currentUser, loginWithToken, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}