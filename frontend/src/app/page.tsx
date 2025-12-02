import { SignInButton, SignUpButton, SignedIn, SignedOut } from '@clerk/nextjs';
import { redirect } from 'next/navigation';
import { auth } from '@clerk/nextjs/server';
import Image from 'next/image';

export default async function Home() {
  const { userId } = await auth();

  if (userId) {
    redirect('/chat');
  }

  return (
    <>
      <SignedOut>
        <div className="min-h-screen bg-gradient-to-br from-purple-600 to-blue-500 flex items-center justify-center">
          <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md w-full">
            <div className="flex justify-center mb-2">
              <Image src="/black_buzzlink.png" alt="BuzzLink Logo" width={160} height={120} />
            </div>
            <p className="text-center text-gray-600 mb-8">Enterprise Chat Application</p>
            <div className="space-y-4">
              <SignInButton mode="redirect" forceRedirectUrl="/chat">
                <button className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition font-semibold">
                  Sign In
                </button>
              </SignInButton>
              <SignUpButton mode="redirect" forceRedirectUrl="/chat">
                <button className="w-full bg-purple-600 text-white py-3 rounded-lg hover:bg-purple-700 transition font-semibold">
                  Sign Up
                </button>
              </SignUpButton>
            </div>
            <div className="mt-8 pt-8 border-t border-gray-200">
              <h3 className="font-semibold text-gray-800 mb-2">Features:</h3>
              <ul className="text-sm text-gray-600 space-y-1">
                <li>✓ Real-time messaging with WebSockets</li>
                <li>✓ Channel-based conversations</li>
                <li>✓ Typing indicators & presence</li>
                <li>✓ Emoji reactions</li>
                <li>✓ File sharing support</li>
                <li>✓ Admin moderation tools</li>
              </ul>
            </div>
          </div>
        </div>
      </SignedOut>
    </>
  );
}
