import React from 'react'
import Dashboard from '../Components/Dashboard'
import { UseUser } from '../hooks/UseUser'

const Category = () => {

  UseUser();

  return (
    <Dashboard activeMenu="Category">
      <div className="my-5 mx-auto">
        {/* add button to add category */}
        {/* catgeory list */}
        {/* adding category modal */}
        {/* updating category modal */}
      </div>
    </Dashboard>
  )
}

export default Category