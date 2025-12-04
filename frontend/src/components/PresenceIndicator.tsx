'use client';

interface PresenceIndicatorProps {
  onlineCount: number;
}

export default function PresenceIndicator({ onlineCount }: PresenceIndicatorProps) {
  return (
    <div className="px-4 py-2 bg-gray-100 border-b border-gray-200 flex items-center space-x-2">
      <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
      <span className="text-sm text-gray-600">
        {onlineCount} {onlineCount === 1 ? 'user' : 'users'} online
      </span>
    </div>
  );
}
