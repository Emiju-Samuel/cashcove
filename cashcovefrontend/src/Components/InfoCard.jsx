import React from 'react'

const InfoCard = ({icon, label, value, color}) => {
  return (
    <div className="flex gap-6 bg-white p-6 rounded-2xl shadow-md shadow-gray-50 border border-gray-200/50 items-center">
        <div className={`w-10 h-10 p-3 flex items-center justify-center text-[20px] text-white ${color} rounded-full drop-shadow-sm`}>
            {icon}
        </div>
        <div className="flex flex-col gap items-start justify-start">
            <h6 className="text-sm text-gray-500 mb-1">{label}</h6>
            <span className="text-[16px] text-medium">₦{value}</span>
        </div>
    </div>
  )
}

export default InfoCard