import React from "react"
import { Container, Typography, Grid, Button, TextField, Card, CardContent, CardActions, Alert } from "@mui/material"
import { Header, Wrapper } from "../../components/core"
import { authService } from "../../service"
import { useNavigate } from "react-router-dom"
import { TokenHelper } from "../../helper";
import SweetAlert from "sweetalert2";

const Register = () => {
  const navigate = useNavigate()
  const [error, setError] = React.useState("")
  const [data, setData] = React.useState({
    username: "",
    password: "",
    confirm_password: ""
  })

  const handleCredentials = credentialType => event => {
    setData({ ...data, [credentialType]: event.target.value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    if (data.password === data.confirm_password) {
      authService.register({ username: data.username, password: data.password }).then(info => {
        if (info.error) {
          SweetAlert.fire({
            icon: "error",
            title: "Error",
            text: info.error
          })
        } else {
          navigate('/login?registered=true')
        }
      })
    } else {
      setError("Invalid password")
    }
  }

  React.useEffect(() => {
    if (TokenHelper.tokenExists()) {
      navigate('/process')
    }
  }, [])

  return (
    <Wrapper>
      <Header />
      <Container maxWidth="md" sx={{ paddingTop: 16 }}>
        <Card elevation={8}>
          <form onSubmit={handleSubmit}>
            <CardContent>
              {error.length > 0 && (
                <Alert severity="error" onClose={() => setError("")}>{error}</Alert>
              )}
              <Grid container spacing={2} justifyContent="center" alignItems="center">
                <Grid item xs={12}>
                  <Typography variant="title" component="h2" align="center" sx={{ color: "black" }}>Register</Typography>
                </Grid>
                <Grid item xs={12}>
                  <TextField value={data.username} onChange={handleCredentials("username")} fullWidth variant="outlined" name="username" label="Username" type="text" />
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField value={data.password} onChange={handleCredentials("password")} fullWidth variant="outlined" name="password" label="Password" type="password" />
                </Grid>
                <Grid item xs={12} md={6}>
                  <TextField value={data.confirm_password} onChange={handleCredentials("confirm_password")} fullWidth variant="outlined" name="confirm_password" label="Confirm password" type="password" />
                </Grid>
              </Grid>
            </CardContent>
            <CardActions>
              <Button size="large" type="submit" variant="contained" fullWidth>Register</Button>
              <Button size="large" type="button" onClick={() => navigate('/login')} fullWidth>Already have an account?</Button>
            </CardActions>
          </form>
        </Card>
      </Container>
    </Wrapper>
  )
}

export default Register
