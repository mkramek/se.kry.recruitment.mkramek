import React from "react"
import axios from "axios"
import { TokenHelper } from "../helper";

const getProcesses = () => {
  return axios.get('/api/v1/service', { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const updateProcess = (process) => {
  return axios.put(`/api/v1/service/${process.id}`, process, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const createProcess = (process) => {
  return axios.post('/api/v1/service', process, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const deleteProcess = (id) => {
  return axios.delete(`/api/v1/service/${id}`, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

export default {
  getProcesses,
  updateProcess,
  createProcess,
  deleteProcess
}
