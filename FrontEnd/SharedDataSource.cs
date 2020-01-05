using System;
using System.Collections.Generic;

namespace FrontEnd
{
    /// <summary>
    /// This will be the shared data source for the system.
    ///
    /// The message queue is the queue of messages to be sent to the server.
    /// The network queue is the queue of responses from the server.
    /// The user queue is the queue of responses that affect the user.
    /// </summary>
    public class SharedDataSource
    {
        private static SharedDataSource _instance = null;
        private Queue<string> _messageQueue;
        public bool SocketDied { get; set; } = false;
        public ClientState ClientState { get; set; }
        public string ClientKey { get; set; }
        public event EventHandler InterfaceUpdate;
        private Queue<string> _userQueue;

        /// <summary>
        /// Constructor for the singleton class to allow for the queues to be created.
        /// </summary>
        private SharedDataSource()
        {
            _messageQueue = new Queue<string>();
            _userQueue = new Queue<string>();
        }

        /// <summary>
        /// Singleton accessor.
        /// </summary>
        /// <returns>The instance of the class.</returns>
        public static SharedDataSource GetInstance()
        {
            return _instance ?? (_instance = new SharedDataSource());
        }

        /// <summary>
        /// Adds a message to be sent to the server.
        /// </summary>
        /// <param name="message">The message to be added.</param>
        public void AddMessage(string message)
        {
            lock (_messageQueue)
            {
                _messageQueue.Enqueue(message);
            }
        }

        /// <summary>
        /// Get a message from the list of messages to be sent.
        /// </summary>
        /// <returns>A message to be sent to the server.</returns>
        public string GetMessage()
        {
            string message;
            lock (_messageQueue)
            {
                message = _messageQueue.Count > 0 ? _messageQueue.Dequeue() : null;
            }
            
            return message;
        }

        /// <summary>
        /// Allows for the message queue to be reset should this be necessary.
        /// </summary>
        public void EmptyMessageQueue()
        {
            lock (_messageQueue)
            {
                _messageQueue.Clear();
            }
        }

        /// <summary>
        /// Fires off an event handler, if one is subscribed, to process a message when it is added.
        /// </summary>
        private void OnAddToUserQueue()
        {
            InterfaceUpdate?.Invoke(this, EventArgs.Empty);
        }

        /// <summary>
        /// Adds a response to the user queue and activates the event that says that this has been added.
        /// </summary>
        /// <param name="userResponse">The response from the server.</param>
        public void AddUserQueue(string userResponse)
        {
            lock (_userQueue)
            {
                _userQueue.Enqueue(userResponse);
            }
            OnAddToUserQueue();
        }

        /// <summary>
        /// Gets a response from the user queue.
        /// </summary>
        /// <returns>A response from the server.</returns>
        public string GetUserQueue()
        {
            string response;
            lock (_userQueue)
            {
                response = _userQueue.Dequeue();
            }

            return response;
        }
    }
}