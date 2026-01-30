import React from 'react'
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from 'recharts';

const CustomPieChart = ({ data, colors, label, totalAmount, showTextAnchor }) => {
  return (
    <div className="flex flex-col items-center justify-center">
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, amount }) => `${name}: ${amount}`}
            outerRadius={80}
            fill="#8884d8"
            dataKey="amount"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
            ))}
          </Pie>
          <Tooltip 
            formatter={(value) => `$${value.toLocaleString()}`}
            contentStyle={{ backgroundColor: '#f5f5f5', border: '1px solid #ccc' }}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
      
      <div className="mt-4 text-center">
        <p className="text-sm text-gray-600">{label}</p>
        <p className="text-2xl font-bold">${totalAmount}</p>
      </div>

      <div className="mt-4 w-full">
        {data && data.map((item, index) => (
          <div key={index} className="flex items-center justify-between py-2 border-b">
            <div className="flex items-center gap-2">
              <div 
                className="w-4 h-4 rounded" 
                style={{ backgroundColor: colors[index % colors.length] }}
              ></div>
              <span className="text-sm">{item.name}</span>
            </div>
            <span className="font-semibold">${item.amount.toLocaleString()}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default CustomPieChart