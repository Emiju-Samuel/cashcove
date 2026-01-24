import React from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'

const Income = () => {
  UseUser();
  return (
    <Dashboard activeMenu="Income">
      This is the income page page
    </Dashboard>
  )
}

export default Income