import React from "react"
import { Container, Typography, Grid, Button, Paper } from "@mui/material"
import { useNavigate } from "react-router-dom";

const Main = () => {
  const navigate = useNavigate();
  return (
    <Container sx={{ height: "100vh", display: "flex", justifyContent: "center", alignItems: "center" }}>
      <Grid container spacing={1}>
        <Grid item xs={12} md={6}>
          <Grid container sx={{ height: "100%" }} justifyContent="center" alignItems="center" direction="column">
            <Grid item>
              <Typography variant="title" component="h1" sx={{ color: "white" }}>Service</Typography>
            </Grid>
            <Grid item>
              <Typography variant="title" component="h1" sx={{ color: "white" }}>Manager</Typography>
            </Grid>
          </Grid>
        </Grid>
        <Grid item xs={12} md={6}>
          <Grid container justifyContent="center" alignItems="center">
            <Grid item>
              <Paper sx={{ padding: 2, paddingTop: 16, paddingBottom: 16 }} elevation={8}>
                <Typography align="center" variant="subtitle" component="h2">Track the status of your services via HTTP protocol.</Typography>
              </Paper>
            </Grid>
          </Grid>
        </Grid>
        <Grid item xs={12} sx={{ textAlign: "center" }}>
          <Button sx={{ marginRight: "4px", width: "calc(50% - 4px)" }} size="large" onClick={() => navigate('/register')} variant="contained">Try it out!</Button>
          <Button sx={{ marginLeft: "4px", width: "calc(50% - 4px)" }} size="large" onClick={() => navigate('/login')} variant="text">Already registered</Button>
        </Grid>
      </Grid>
    </Container>
  )
}

export default Main
