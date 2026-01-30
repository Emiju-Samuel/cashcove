import React from 'react'
import { UseUser } from '../hooks/UseUser'
import Dashboard from '../Components/Dashboard';

const Subscription = () => {

    UseUser();

  return (
    <Dashboard activeMenu="Subscriptions">
        <div>Manage your subscriptions easily with CashCove .... feature is coming soon</div>
    </Dashboard>
  )
}

export default Subscription