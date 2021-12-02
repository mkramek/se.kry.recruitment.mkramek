import React from "react"
import {
  Toolbar,
  Typography,
  AppBar,
  Button
} from "@mui/material"
import {Logout as LogoutIcon, Settings as SettingsIcon} from "@mui/icons-material"
import { useNavigate } from "react-router-dom"
import { TokenHelper } from "../../helper";
import {UserSettingsDialog} from "../dialog";

const Header = () => {
  const navigate = useNavigate()
  const [settingsOpened, openSettings] = React.useState(false)

  const handleLogout = () => {
    TokenHelper.removeToken()
    navigate('/')
  }

  const handleSettings = () => {
    openSettings(true)
  }

  const handleClose = () => {
    openSettings(false)
  }

  return (
    <AppBar position="fixed">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>Service Manager</Typography>
        {TokenHelper.tokenExists() && (
          <>
            <Button onClick={handleSettings} size="large" startIcon={<SettingsIcon sx={{ color: "#fff" }} />} sx={{ color: "#fff" }}>Settings</Button>
            <Button onClick={handleLogout} size="large" startIcon={<LogoutIcon sx={{ color: "#fff" }} />} sx={{ color: "#fff" }}>Logout</Button>
          </>
        )}
      </Toolbar>
      <UserSettingsDialog open={settingsOpened} onClose={handleClose} />
    </AppBar>
  )
}

export default Header
