import {userService} from "../service";

const tokenExists = () => {
  const token = localStorage.getItem("auth_token")
  return token && token.length > 0
}
const getToken = () => {
  return localStorage.getItem("auth_token")
}
const setToken = (authToken) => {
  localStorage.setItem("auth_token", authToken)
}
const removeToken = () => {
  localStorage.removeItem("auth_token")
}

const getOwner = () => {
  return userService.getInfo()
}

export default {
  tokenExists,
  getToken,
  setToken,
  removeToken,
  getOwner
}
