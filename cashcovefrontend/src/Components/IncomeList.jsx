import { Download, LoaderCircle, Mail } from 'lucide-react'
import React, { useState } from 'react'
import moment from 'moment'
import TransactionInfoCard from './TransactionInfoCard'

const IncomeList = ({transactions, onDelete, onDownload, onEmail}) => {

    const [isEmailing, setIsEmailing] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);

    const handleEmail = async () => {
        setIsEmailing(true);
        try{
            await onEmail();
        }finally{
            setIsEmailing(false);
        }
    }

    const handleDownload = async () => {
        setIsDownloading(true);
        try{
            await onDownload();
        }finally{
            setIsDownloading(false);
        }
    }

  return (
    <div className="card mt-5 mb-8 bg-white py-5 px-3 sm:px-8 rounded-xl shadow-md shadow-gray-50 border border-gray-200/50 gap-3 sm:gap-6 flex flex-col">
        <div className="flex items-center justify-between">
            <h5 className="sm:text-lg text-base">Income Sources</h5>
            <div className="flex items-center justify-end gap-1 sm:gap-6">
                <button disabled={isEmailing} className="card-btn flex items-center justify-end gap-2" onClick={handleEmail}>
                    {isEmailing ? (
                        <>
                        <LoaderCircle className='w-4 h-4 animate-spin'/>
                        Emailing....
                        </>
                    ):(
                       <>
                       <Mail size={15} className='text-base invisible sm:visible'/> Email
                       </> 
                    )}
                </button>

                <button disabled={isDownloading} className="card-btn flex items-center justify-end gap-2" onClick={handleDownload}>
                    {isDownloading ? (
                        <>
                        <LoaderCircle className='w-4 h-4 animate-spin'/>
                        Downloading...
                        </>
                    ):(
                        <>
                        <Download size={15} className='text-base invisible sm:visible'/> Download
                        </>
                    )}
                </button>
            </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2">
            {/* display the incomes */}
            {transactions?.map((income)=> (
                <TransactionInfoCard
                key={income.id}
                title={income.name}
                icon={income.icon}
                date={moment(income.date).format("Do MMM YYYY")}
                amount={income.amount}
                type="income"
                onDelete={()=>onDelete(income.id)}
                />
            ))}
        </div>
    </div>
  )
}

export default IncomeList