import { AlertCircle } from 'lucide-react';
import RenewalCountdown from './RenewalCountdown';

const UpcomingRenewalCard = ({ subscription, onHandle }) => {
  const { icon, subscriptionName, amount, nextRenewalDate } = subscription;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const getDaysLeft = () => {
    const now = new Date();
    const renewal = new Date(nextRenewalDate);
    const diff = renewal - now;
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  };

  const daysLeft = getDaysLeft();
  const isUrgent = daysLeft <= 3;

  return (
    <div
      className={`relative rounded-lg p-6 shadow-md transition-all ${
        isUrgent
          ? 'bg-gradient-to-r from-red-50 to-red-100 ring-2 ring-red-300'
          : 'bg-gradient-to-r from-yellow-50 to-yellow-100 ring-2 ring-yellow-300'
      }`}
    >
      {isUrgent && (
        <div className="absolute top-3 right-3 flex items-center gap-1 bg-red-500 text-white text-xs font-bold px-3 py-1 rounded-full">
          <AlertCircle size={14} /> Urgent
        </div>
      )}

      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="text-4xl">{icon || '📦'}</div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">{subscriptionName}</h3>
            <p className="text-sm text-gray-600">{formatCurrency(amount)}</p>
          </div>
        </div>
      </div>

      <div className="mb-4">
        <p className="text-xs text-gray-600 mb-2">Renewal in</p>
        <div className="text-3xl font-bold text-gray-900 mb-1">
          {daysLeft} day{daysLeft !== 1 ? 's' : ''}
        </div>
        <RenewalCountdown nextRenewalDate={nextRenewalDate} />
      </div>

      <button
        onClick={() => onHandle(subscription)}
        className="w-full mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition"
      >
        Handle Now
      </button>
    </div>
  );
};

export default UpcomingRenewalCard;
