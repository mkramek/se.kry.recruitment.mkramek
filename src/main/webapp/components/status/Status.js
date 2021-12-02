import React from "react";
import { Chip } from "@mui/material";

const OK = "ok"
const FAIL = "fail"
const UNKNOWN = "unknown"
const StatusChip = ({ status }) => {
  let colorType;
  switch (status) {
    case OK:
      colorType = "success"
      break
    case FAIL:
      colorType = "error"
      break
    default:
      colorType = "info"
      break
  }
  return (
    <Chip color={colorType} label={status.toUpperCase()} />
  )
}

export default {
  OK,
  FAIL,
  UNKNOWN,
  StatusChip
}
