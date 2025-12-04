'use client';

import { useEffect, useState, useRef } from 'react';
import { useUser } from '@clerk/nextjs';
import { Message } from '@/types';
import { getThreadReplies } from '@/lib/api';
import MessageList from './MessageList';
import MessageInput from './MessageInput';

interface ThreadViewProps {
    parentMessage: Message;
    onClose: () => void;
    onSendReply: (content: string, type: 'TEXT' | 'FILE') => void;
    newReplies?: Message[]; // Real-time replies from WebSocket
}

export default function ThreadView({ parentMessage, onClose, onSendReply, newReplies = [] }: ThreadViewProps) {
    const { user } = useUser();
    const [replies, setReplies] = useState<Message[]>([]);
    const [loading, setLoading] = useState(true);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [replies]);

    useEffect(() => {
        const loadReplies = async () => {
            setLoading(true);
            try {
                const fetchedReplies = await getThreadReplies(parentMessage.id);
                setReplies(fetchedReplies);
            } catch (error) {
                console.error('Error loading thread replies:', error);
            } finally {
                setLoading(false);
            }
        };

        loadReplies();
    }, [parentMessage.id]);

    // Handle new real-time replies
    useEffect(() => {
        if (newReplies && newReplies.length > 0) {
            setReplies((prev) => {
                // Merge new replies, avoiding duplicates
                const existingIds = new Set(prev.map(r => r.id));
                const uniqueNewReplies = newReplies.filter(r => !existingIds.has(r.id));
                return [...prev, ...uniqueNewReplies];
            });
        }
    }, [newReplies]);

    return (
        <div className="flex-1 flex flex-col bg-white border-l border-gray-200">
            {/* Thread Header */}
            <div className="border-b border-gray-200 px-6 py-4 flex items-center justify-between">
                <div>
                    <h2 className="text-xl font-semibold text-gray-800">Thread</h2>
                    <p className="text-sm text-gray-500">
                        {replies.length} {replies.length === 1 ? 'reply' : 'replies'}
                    </p>
                </div>
                <button
                    onClick={onClose}
                    className="p-2 hover:bg-gray-100 rounded-full transition"
                    title="Close thread"
                >
                    <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            {/* Parent Message */}
            <div className="border-b border-gray-200 bg-gray-50 px-6 py-4">
                <div className="flex items-start space-x-3">
                    <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold">
                        {parentMessage.sender.displayName.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1">
                        <div className="flex items-baseline space-x-2">
                            <span className="font-semibold text-gray-900">{parentMessage.sender.displayName}</span>
                            <span className="text-xs text-gray-500">
                                {new Date(parentMessage.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                        </div>
                        <p className="text-gray-800 mt-1">{parentMessage.content}</p>
                    </div>
                </div>
            </div>

            {/* Thread Replies */}
            {loading ? (
                <div className="flex-1 flex items-center justify-center">
                    <div className="text-gray-500">Loading replies...</div>
                </div>
            ) : (
                <>
                    <div className="flex-1 overflow-y-auto px-6 py-4">
                        {replies.length === 0 ? (
                            <div className="text-center text-gray-500 py-8">
                                No replies yet. Be the first to reply!
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {replies.map((reply) => (
                                    <div key={reply.id} className="flex items-start space-x-3">
                                        <div className="w-8 h-8 rounded-full bg-green-500 flex items-center justify-center text-white text-sm font-semibold">
                                            {reply.sender.displayName.charAt(0).toUpperCase()}
                                        </div>
                                        <div className="flex-1">
                                            <div className="flex items-baseline space-x-2">
                                                <span className="font-semibold text-gray-900 text-sm">{reply.sender.displayName}</span>
                                                <span className="text-xs text-gray-500">
                                                    {new Date(reply.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                </span>
                                            </div>
                                            <p className="text-gray-800 mt-1">{reply.content}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>
                </>
            )}

            {/* Reply Input */}
            <div className="border-t border-gray-200">
                <MessageInput
                    onSendMessage={onSendReply}
                    onTyping={() => { }} // No typing indicator for threads
                />
            </div>
        </div>
    );
}
