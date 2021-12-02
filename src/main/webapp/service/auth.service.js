import React from "react"
import axios from "axios"

const login = (credentials) => {
  return axios.post("/api/v1/auth/login", credentials)
    .then(response => {
      const { data } = response
      return data.error ? { error: data.error } : { auth_token: data.auth_token }
    })
}

const register = (credentials) => {
  return axios.post("/api/v1/user", credentials)
    .then(response => {
      const { data } = response
      if (data.error) {
        return {
          error: data.error
        }
      } else {
        return data
      }
    })
}

export default {
  login,
  register
}
