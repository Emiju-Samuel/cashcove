import { Download, Mail } from 'lucide-react'
import React from 'react'
import moment from 'moment'
import TransactionInfoCard from './TransactionInfoCard'

const IncomeList = ({transactions, onDelete}) => {
  return (
    <div className="card">
        <div className="flex items-center justify-between">
            <h5 className="text-lg">Income Sources</h5>
            <div className="flex items-center justify-end gap-2">
                <button className="card-btn flex items-center justify-end gap-2">
                    <Mail size={15} className='text-base'/> Email
                </button>

                <button className="card-btn flex items-center justify-end gap-2">
                    <Download size={15} className='text-base'/> Download
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
                type="Income"
                onDelete={()=>onDelete(income.id)}
                />
            ))}
        </div>
    </div>
  )
}

export default IncomeList