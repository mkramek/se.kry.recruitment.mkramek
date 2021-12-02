import React from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, TextField } from "@mui/material";
import { processService } from "../../service";

const NewProcessDialog = ({ open, onClose }) => {
  const [error, setError] = React.useState("")
  const [newProcess, setNewProcess] = React.useState({
    name: "",
    url: ""
  })

  const handleChange = name => event => {
    setNewProcess({ ...newProcess, [name]: event.target.value })
  }

  const handleOnProcessCreate = (event) => {
    event.preventDefault()
    processService.createProcess(newProcess).then(result => {
      if (result.error) {
        setError(result.error)
      } else {
        onClose()
        window.location.reload()
      }
    })
  }

  return (
    <Dialog open={open} onClose={onClose}>
      <form onSubmit={handleOnProcessCreate}>
        <DialogTitle>New process</DialogTitle>
        <DialogContent>
          {error.length > 0 && (
            <Alert severity="error" onClose={() => setError("")}>{error}</Alert>
          )}
          <Grid container spacing={2} sx={{ marginTop: 1 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Process name" variant="outlined" name="name" type="text" value={newProcess.name} onChange={handleChange("name")} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Process URL" variant="outlined" name="url" type="text" value={newProcess.url} onChange={handleChange("url")} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button type="submit" size="large">Create</Button>
          <Button type="button" size="large" onClick={onClose} color="error">Cancel</Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}

export default NewProcessDialog
