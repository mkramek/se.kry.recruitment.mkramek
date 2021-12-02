import React from "react";
import ReactDOM from "react-dom";
import { MainRouter } from "./components";
import { CssBaseline, ThemeProvider } from "@mui/material";
import { theme } from "./assets";

const Application = () => (
  <ThemeProvider theme={theme}>
    <CssBaseline />
    <MainRouter />
  </ThemeProvider>
);

ReactDOM.render(<Application />, document.getElementById("app-root"));
