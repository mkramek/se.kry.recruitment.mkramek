import React from "react"
import { Container, Typography, Grid, Button, TextField, Card, CardContent, CardActions, Alert } from "@mui/material"
import { Header, Wrapper } from "../../components/core"
import { authService } from "../../service"
import { useLocation, useNavigate } from "react-router-dom"
import SweetAlert from "sweetalert2";
import { TokenHelper } from "../../helper";

const Login = () => {

  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = React.useState("")
  const [info, setInfo] = React.useState("")
  const [credentials, setCredentials] = React.useState({
    username: "",
    password: ""
  })

  const handleCredentials = credentialType => event => {
    setCredentials({ ...credentials, [credentialType]: event.target.value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    authService.login(credentials).then(result => {
      if (result.error) {
        SweetAlert.fire({
          icon: "error",
          title: "Error",
          text: result.error
        })
      } else {
        TokenHelper.setToken(result.auth_token)
        navigate('/process')
      }
    })
  }

  React.useEffect(() => {
    const urlParams = new URLSearchParams(location.search)
    if (urlParams.get("error_message")) {
      setError(urlParams.get("error_message"))
    } else if (urlParams.get("registered")) {
      setInfo("Account has been created. You can login now!")
    }
  }, [])

  React.useEffect(() => {
    if (TokenHelper.tokenExists()) {
      navigate('/process')
    }
  }, [])

  return (
    <Wrapper>
      <Header />
      <Container maxWidth="md" sx={{ paddingTop: 16 }}>
        <Card>
          <form onSubmit={handleSubmit}>
            <CardContent>
              {error.length > 0 && (
                <Alert severity="error" onClose={() => setError("")}>{error}</Alert>
              )}
              {info.length > 0 && (
                <Alert severity="success" onClose={() => setError("")}>{info}</Alert>
              )}
              <Grid container spacing={2} justifyContent="center" alignItems="center">
                <Grid item xs={12}>
                  <Typography variant="title" component="h2" align="center" sx={{ color: "black" }}>Login</Typography>
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField label="Username" value={credentials.username} onChange={handleCredentials("username")} fullWidth variant="outlined" name="username" type="text" />
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField label="Password" value={credentials.password} onChange={handleCredentials("password")} fullWidth variant="outlined" name="password" type="password" />
                </Grid>
              </Grid>
            </CardContent>
            <CardActions>
              <Button size="large" type="submit" variant="contained" fullWidth>Login</Button>
              <Button size="large" type="button" onClick={() => navigate('/register')} fullWidth>Register</Button>
            </CardActions>
          </form>
        </Card>
      </Container>
    </Wrapper>
  )
}

export default Login
