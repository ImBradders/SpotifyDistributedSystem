using System;
using System.Collections.Generic;

namespace FrontEnd
{
    public delegate void InterfaceUpdateHandler();
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
        private LinkedList<string> _messageQueue;
        public bool SocketDied { get; set; } = false;
        private ServerType _currentServerType;
        public ClientState ClientState { get; set; }
        private Queue<string> _userQueue;

        public event InterfaceUpdateHandler Updated;

        protected virtual void OnInterfaceUpdate()
        {
            InterfaceUpdateHandler handler = Updated;

            if (handler != null)
            {
                handler();
            }
        }

        /// <summary>
        /// Constructor for the singleton class to allow for the queues to be created.
        /// </summary>
        private SharedDataSource()
        {
            _messageQueue = new LinkedList<string>();
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
        
        public ServerType CurrentServerType
        {
            get => _currentServerType;
            set => _currentServerType = value;
        }

        /// <summary>
        /// Adds a message to be sent to the server.
        /// </summary>
        /// <param name="message">The message to be added.</param>
        public void AddMessage(string message)
        {
            lock (_messageQueue)
            {
                _messageQueue.AddLast(message);
            }
        }

        /// <summary>
        /// There are times where it is necessary for certain messages to be processed first rather than checking for more items in the list. This ensures that.
        /// </summary>
        /// <param name="message">The message to be added.</param>
        public void AddMessageStart(string message)
        {
            lock (_messageQueue)
            {
                _messageQueue.AddFirst(message);
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
                if (_messageQueue.Count > 0)
                {
                    message = _messageQueue.First.Value;
                    _messageQueue.RemoveFirst();
                }
                else
                {
                    message = null;
                }
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
        /// Adds a response to the user queue and activates the event that says that this has been added.
        /// </summary>
        /// <param name="userResponse">The response from the server.</param>
        public void AddUserQueue(string userResponse)
        {
            lock (_userQueue)
            {
                _userQueue.Enqueue(userResponse);
            }

            OnInterfaceUpdate();
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