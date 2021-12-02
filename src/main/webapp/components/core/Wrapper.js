import React from "react"
import { Box } from "@mui/material"

const Wrapper = (props) => {
  return (
    <Box
      sx={{
        width: '100vw',
        minHeight: '100vh'
      }}
    >
      { props.children }
    </Box>
  )
}

export default Wrapper
