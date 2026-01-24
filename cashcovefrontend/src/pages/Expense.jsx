import React from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'

const Expense = () => {
  UseUser();
  return (
    <Dashboard activeMenu="Expenses">
      This is the expense page
    </Dashboard>
  )
}

export default Expense