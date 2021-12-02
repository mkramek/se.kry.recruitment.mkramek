import React from "react"
import axios from "axios"
import { TokenHelper } from "../helper";

const getUsers = () => {
  return axios.get("/api/v1/user", { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const getUser = (id) => {
  return axios.get(`/api/v1/user/${id}`, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const updateUser = (user) => {
  return axios.put(`/api/v1/user/${user.id}`, user, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const deleteUser = (id) => {
  return axios.delete(`/api/v1/user/${id}`, { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

const getInfo = () => {
  return axios.get("/api/v1/auth/info", { headers: { Authorization: `Bearer ${TokenHelper.getToken()}` } }).then(response => response.data)
}

export default {
  getUsers,
  getUser,
  getInfo,
  updateUser,
  deleteUser
}
