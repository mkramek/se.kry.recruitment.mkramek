import React from "react"
import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import { Main, Default } from "../../pages/core"
import { Processes } from "../../pages/process"
import { Login, Register } from "../../pages/auth"
import { AuthRoute } from "../routing"

export default function MainRouter() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/process"
          element={
            <AuthRoute>
              <Processes />
            </AuthRoute>
          }
        />
        <Route element={<Default />} />
      </Routes>
    </Router>
  )
}
