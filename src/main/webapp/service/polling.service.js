import React from "react"
import axios from "axios"
import { Status } from "../components/status";

const poll = (process, timeout) => {
  const TIMEOUT_ERROR_MESSAGE = "Request timed out"
  axios.defaults.timeout = timeout
  axios.defaults.timeoutErrorMessage = TIMEOUT_ERROR_MESSAGE
  return axios.get(process.url)
    .then(response => {
      if ([200, 201, 202, 204].indexOf(response.status) >= 0) {
        return {
          process: process.id,
          processStatus: Status.OK
        }
      } else {
        return {
          process: process.id,
          processStatus: Status.UNKNOWN,
          reason: "Unknown response"
        }
      }
    }, error => {
      if (error.message === TIMEOUT_ERROR_MESSAGE) {
        return {
          process: process.id,
          processStatus: Status.FAIL,
          reason: TIMEOUT_ERROR_MESSAGE
        }
      } else if (!error.response) {
        return {
          process: process.id,
          processStatus: Status.FAIL,
          reason: "Network or unknown error"
        }
      } else {
        return {
          process: process.id,
          processStatus: Status.FAIL,
          reason: `HTTP ${error.response.status} ${error.response.statusText}`
        }
      }
    })
}

export default {
  poll
}
