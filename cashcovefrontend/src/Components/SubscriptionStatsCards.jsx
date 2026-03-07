import { TrendingUp } from 'lucide-react';

const SubscriptionStatsCards = ({ subscriptions }) => {
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Calculate statistics
  const activeSubscriptions = subscriptions.filter((s) => s.subscriptionStatus === 'ACTIVE').length;

  const freqMultiplier = {
    MONTHLY: 12,
    QUARTERLY: 4,
    SEMI_ANNUAL: 2,
    YEARLY: 1,
  };

  const monthlySpend = subscriptions
    .filter((s) => s.subscriptionStatus === 'ACTIVE')
    .reduce((sum, s) => {
      const amount = typeof s.amount === 'string' ? parseFloat(s.amount) : (s.amount || 0);
      const multiplier = freqMultiplier[s.frequency] || 1;
      return sum + (amount * multiplier) / 12;
    }, 0);

  const annualSpend = subscriptions
    .filter((s) => s.subscriptionStatus === 'ACTIVE')
    .reduce((sum, s) => {
      const amount = typeof s.amount === 'string' ? parseFloat(s.amount) : (s.amount || 0);
      const multiplier = freqMultiplier[s.frequency] || 1;
      return sum + amount * multiplier;
    }, 0);

  const averagePerSub = activeSubscriptions > 0 ? annualSpend / activeSubscriptions / 12 : 0;

  const stats = [
    {
      title: 'Active Subscriptions',
      value: activeSubscriptions.toString(),
      icon: '📦',
      color: 'bg-blue-50',
      borderColor: 'border-blue-200',
    },
    {
      title: 'Monthly Spend',
      value: formatCurrency(monthlySpend),
      icon: '💳',
      color: 'bg-green-50',
      borderColor: 'border-green-200',
    },
    {
      title: 'Annual Spend',
      value: formatCurrency(annualSpend),
      icon: '📊',
      color: 'bg-purple-50',
      borderColor: 'border-purple-200',
    },
    {
      title: 'Average per Sub',
      value: formatCurrency(averagePerSub),
      icon: '⚖️',
      color: 'bg-orange-50',
      borderColor: 'border-orange-200',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, index) => (
        <div
          key={index}
          className={`${stat.color} ${stat.borderColor} border rounded-lg p-5 transition-all hover:shadow-md`}
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 font-medium">{stat.title}</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">{stat.value}</p>
            </div>
            <div className="text-3xl">{stat.icon}</div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default SubscriptionStatsCards;
