'use client';

import { useState, useEffect } from 'react';
import { useUser } from '@clerk/nextjs';
import { updateProfile, getCurrentUser } from '@/lib/api';
import Header from '@/components/Header';
import Link from 'next/link';

export default function ProfilePage() {
  const { user, isLoaded } = useUser();
  const [displayName, setDisplayName] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (isLoaded && user) {
      setDisplayName(user.fullName || user.username || '');
      setAvatarUrl(user.imageUrl || '');

      getCurrentUser().catch(console.error);
    }
  }, [isLoaded, user]);

  const handleSave = async () => {
    if (!displayName.trim()) {
      setMessage('Display name is required');
      return;
    }

    setSaving(true);
    setMessage('');

    try {
      await updateProfile(displayName, avatarUrl);
      setMessage('Profile updated successfully!');
    } catch (error) {
      console.error('Error updating profile:', error);
      setMessage('Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (!isLoaded) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />
      <div className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-bold text-gray-800">Profile Settings</h1>
            <Link href="/chat" className="text-blue-600 hover:underline text-sm">
              Back to Chat
            </Link>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Display Name
              </label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Your display name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Avatar URL
              </label>
              <input
                type="url"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="https://example.com/avatar.jpg"
              />
              <p className="text-xs text-gray-500 mt-1">
                Optional: Provide a URL to your avatar image
              </p>
            </div>

            <button
              onClick={handleSave}
              disabled={saving}
              className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </button>

            {message && (
              <div
                className={`p-3 rounded-lg text-sm ${
                  message.includes('success')
                    ? 'bg-green-100 text-green-800'
                    : 'bg-red-100 text-red-800'
                }`}
              >
                {message}
              </div>
            )}
          </div>

          <div className="mt-6 pt-6 border-t border-gray-200">
            <h3 className="font-semibold text-gray-800 mb-2">Account Info</h3>
            <div className="text-sm text-gray-600 space-y-1">
              <p>
                <span className="font-medium">Email:</span>{' '}
                {user?.primaryEmailAddress?.emailAddress}
              </p>
              <p>
                <span className="font-medium">User ID:</span> {user?.id}
              </p>
              {Boolean(user?.publicMetadata?.isAdmin) && (
                <p className="text-yellow-600 font-medium">Admin Account</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
