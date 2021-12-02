import React from "react"
import {
  Button,
  Container,
  Grid,
  Typography
} from "@mui/material"
import { Add as AddIcon } from "@mui/icons-material"
import { Header, Wrapper } from "../../components/core"
import { processService } from "../../service"
import { ProcessMapper } from "../../components/process"
import { useNavigate } from "react-router-dom"
import { TokenHelper } from "../../helper"
import { NewProcessDialog } from "../../components/dialog"

const Processes = () => {
  const navigate = useNavigate()
  const [processList, setProcessList] = React.useState([])
  const [creatorOpened, setCreatorOpened] = React.useState(false)

  React.useEffect(() => {
    if (!TokenHelper.tokenExists()) {
      navigate("/login?error_message=Unauthorized")
    }
  }, [])

  React.useEffect(() => {
    processService.getProcesses().then(result => {
      if (result.error || result.error === null) {
        TokenHelper.removeToken()
        navigate(`/login?error_message=${result.error}`)
      } else {
        setProcessList(result)
      }
    })
  }, [])

  const handleNewProcess = () => setCreatorOpened(true)

  return (
    <Wrapper>
      <Header />
      <Container maxWidth="lg" sx={{ paddingTop: 16, paddingBottom: 8 }}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Grid container justifyContent="space-between" alignItems="center" spacing={2}>
              <Grid item>
                <Typography variant="title" component="h2" sx={{ color: "white" }}>Your processes</Typography>
              </Grid>
              <Grid item>
                <Button size="large" variant="contained" startIcon={<AddIcon />} onClick={handleNewProcess}>New process</Button>
              </Grid>
            </Grid>
          </Grid>
          <Grid item xs={12}>
            <ProcessMapper processes={processList} />
          </Grid>
        </Grid>
      </Container>
      <NewProcessDialog open={creatorOpened} onClose={() => setCreatorOpened(false)} />
    </Wrapper>
  )
}

export default Processes
