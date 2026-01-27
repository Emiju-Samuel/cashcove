import React from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Home from './pages/Home'
import Income from './pages/Income'
import Expense from './pages/Expense'
import Category from './pages/Category'
import Filter from './pages/Filter'
import Login from './pages/Login'
import Signup from './pages/Signup'
import 'react-toastify/dist/ReactToastify.css'
import { ToastContainer } from 'react-toastify'
import Subscription from './pages/Subscription'

const App = () => {
  return (
    <>
    <ToastContainer />
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<Root />} />
        <Route path='/dashboard' element={<Home />}/>
        <Route path='/income' element={<Income />}/>
        <Route path='/expense' element={<Expense />}/>
        <Route path='/category' element={<Category />}/>
        <Route path='/filter' element={<Filter />}/>
        <Route path='/login' element={<Login />}/>
        <Route path='/signup' element={<Signup />}/>
        <Route path='/subscription' element={<Subscription />} />
      </Routes>
    </BrowserRouter>
    </>
  )
}

const Root = () => {
  const isAuthenticated = !!localStorage.getItem("token");
  return isAuthenticated ? (
    <Navigate to="/dashboard" />
  ):(
    <Navigate to="/login" />
  )
}

export default App