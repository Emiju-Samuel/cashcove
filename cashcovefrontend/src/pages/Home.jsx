import React from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'

const Home = () => {
  UseUser();
  return (
    <div>
      <Dashboard activeMenu="Dashboard">
        This is the home page
      </Dashboard>
    </div>
  )
}

export default Home