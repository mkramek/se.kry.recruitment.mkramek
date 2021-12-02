import React from "react";
import { Grid, Typography } from "@mui/material";
import ProcessCard from "./ProcessCard";

const ProcessMapper = ({ processes }) => {
  if (processes.length === 0) {
    return <Typography variant="body1" component="h4" align="center" sx={{ color: "white" }}>No processes to display.</Typography>
  } else {
    return (
      <Grid container spacing={2}>
        {!processes.error && processes.map((process, idx) => {
          return (
            <Grid item key={idx} xs={12} md={6}>
              <ProcessCard process={process} />
            </Grid>
          )
        })}
      </Grid>
    )
  }
}

export default ProcessMapper;
