import React from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'

const Filter = () => {
  UseUser();
  return (
    <Dashboard activeMenu="Filters">
      This is the filter page
    </Dashboard>
  )
}

export default Filter