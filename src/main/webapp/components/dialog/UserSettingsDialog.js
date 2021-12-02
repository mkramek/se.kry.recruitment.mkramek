import React from "react";
import { userService } from "../../service";
import { TokenHelper } from "../../helper";
import {
  Alert,
  Button, Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  TextField,
  Typography
} from "@mui/material";

const UserSettingsDialog = ({ open, onClose }) => {
  const [error, setError] = React.useState("")
  const [settings, setSettings] = React.useState({
    password: "",
    confirmPassword: ""
  })

  const handleSettingsSave = (event) => {
    event.preventDefault()
    if (settings.password === settings.confirmPassword) {
      TokenHelper.getOwner().then(owner => {
        userService.updateUser({ id: owner.id, username: owner.username, password: settings.password })
          .then((result) => {
            if (result.error || result.error === null) {
              setError(result.error ?? "Unknown error")
            } else {
              onClose()
              window.location.reload()
            }
          })
      })
    } else {
      setError("Passwords are not matching")
    }
  }

  const handleChange = name => event => {
    setSettings({ ...settings, [name]: event.target.value })
  }

  return (
    <Dialog open={open} onClose={onClose}>
      <form onSubmit={handleSettingsSave}>
        <DialogTitle>Settings</DialogTitle>
        <DialogContent>
          {error.length > 0 && (
            <Alert severity="error" onClose={() => setError("")}>{error}</Alert>
          )}
          <Grid container spacing={2} sx={{ marginTop: 1 }}>
            <Grid item xs={12}>
              <Typography variant="body1" component="h4">Change password</Typography>
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Password" variant="outlined" name="password" type="password" value={settings.name} onChange={handleChange("password")} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Confirm password" variant="outlined" name="confirmPassword" type="password" value={settings.url} onChange={handleChange("confirmPassword")} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button type="submit" size="large">Save</Button>
          <Button
            type="button"
            size="large"
            onClick={() => {
              setSettings({ password: "", confirmPassword: "" })
              onClose()
            }}
            color="error"
          >
            Cancel
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}

export default UserSettingsDialog
