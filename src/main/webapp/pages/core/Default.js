import React from "react"
import { Container, Typography, Grid} from "@mui/material"

const Default = () => {
  return (
    <Container>
      <Grid container justifyContent="center" alignItems="center">
        <Grid item>
          <Typography variant="h1" align="center">Page not found</Typography>
        </Grid>
      </Grid>
    </Container>
  )
}

export default Default
