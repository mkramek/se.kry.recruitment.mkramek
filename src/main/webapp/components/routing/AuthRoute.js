import React from "react"
import axios from "axios"
import { Navigate } from "react-router-dom";
import { TokenHelper } from "../../helper";

const AuthRoute = ({ children }) => {
  const [waitingForAuth, setWaiting] = React.useState(true)
  const [authenticated, setAuthenticated] = React.useState(false)
  if (TokenHelper.tokenExists()) {
    axios.get("/api/v1/auth/verify", { headers: { "Authorization": `Bearer ${TokenHelper.getToken()}` } })
      .then(response => {
        if (response.data.error) {
          setAuthenticated(false)
        } else {
          setAuthenticated(true)
        }
        setWaiting(false)
      })
  } else {
    return <Navigate to="/login?error_message=User not authenticated" />
  }

  return !waitingForAuth ? (authenticated ? children : <Navigate to="/login?error_message=User not authenticated" />) : <div>Loading...</div>
}

export default AuthRoute
