import React from "react"
import { Alert, Button, Card, CardActions, CardContent, TextField, Typography } from "@mui/material"
import { pollingService, processService } from "../../service"
import SweetAlert from "sweetalert2"
import { Status } from "../status"
import { PollingHelper } from "../../helper"

const ProcessCard = ({ process }) => {
  const MAX_TIMEOUT = 5000
  const POLLING_DELAY = 2000
  const [data, setData] = React.useState(process)
  const [pollErrReason, setPollErrReason] = React.useState("")
  const [error, setError] = React.useState("")
  const [editMode, setEditMode] = React.useState(false)

  PollingHelper.useInterval(async () => {
    await pollingService.poll(process, MAX_TIMEOUT).then(result => {
      if (result.processStatus === Status.FAIL) {
        setPollErrReason(result.reason)
      } else {
        setPollErrReason("")
      }
      if (data.lastStatus !== result.processStatus) {
        processService.updateProcess({ ...data, lastStatus: result.processStatus })
        setData({ ...data, lastStatus: result.processStatus })
      }
    })
  }, POLLING_DELAY)

  const handleChange = name => event => {
    setData({ ...data, [name]: event.target.value })
  }

  const handleSave = () => {
    processService.updateProcess(data).then(result => {
      if (result.error) {
        setError(result.error)
      } else {
        setEditMode(false)
        window.location.reload()
      }
    })
  }

  const handleCancel = () => {
    setEditMode(false)
    setData(process)
  }

  const handleDelete = () => {
    SweetAlert.fire({
      icon: "warning",
      text: "Are you sure you want to delete this process?",
      showCancelButton: true,
      confirmButtonColor: "#3085d6",
      cancelButtonColor: "#d33",
      confirmButtonText: "Yes",
      cancelButtonText: "No"
    }).then(result => {
      if (result.isConfirmed) {
        processService.deleteProcess(process.id).then(response => {
          if (response.error) {
            SweetAlert.fire({
              icon: "error",
              text: response.error
            })
          } else {
            window.location.reload()
          }
        })
      }
    })
  }

  return (
    <Card elevation={8}>
      <CardContent>
        <Typography sx={{ fontSize: 14 }} color="text.secondary" gutterBottom>
          process ID: {data.id}
        </Typography>
        {editMode ? (
          <TextField margin="dense" label="Service name" fullWidth name="name" type="text" value={data.name} onChange={handleChange("name")} />
        ) : (
          <Typography variant="h5" component="div">{data.name}</Typography>
        )}
        {editMode ? (
          <TextField margin="dense" label="Service URL" fullWidth name="url" type="text" value={data.url} onChange={handleChange("url")} />
        ) : (
          <Typography sx={{ mb: 1.5 }} color="text.secondary">{data.url}</Typography>
        )}
        <Typography variant="body2" component="span" sx={{ paddingTop: 1, paddingBottom: 1 }}>Last status: </Typography>
        <Status.StatusChip status={data.lastStatus ?? Status.UNKNOWN} sx={{ display: "inline" }} />
        {pollErrReason.length > 0 && (
          <Typography variant="body2" sx={{ paddingTop: 1, paddingBottom: 1 }}>Reason: {pollErrReason}</Typography>
        )}
        <Typography variant="body2" sx={{ paddingTop: 1, paddingBottom: 1 }}>
          Created at: {data.createdAt}
        </Typography>
        {error.length > 0 && <Alert onClose={() => setError("")} severity="error">{error}</Alert>}
      </CardContent>
      <CardActions>
        {!editMode && (
          <>
            <Button size="small" color="info" onClick={() => setEditMode(true)}>Edit</Button>
            <Button size="small" color="error" onClick={handleDelete}>Delete</Button>
          </>
        )}
        {editMode && (
          <>
            <Button size="small" color="info" onClick={handleSave}>Save</Button>
            <Button size="small" color="error" onClick={handleCancel}>Cancel</Button>
          </>
        )}
      </CardActions>
    </Card>
  )
}

export default ProcessCard
