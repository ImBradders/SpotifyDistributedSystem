using System;
using System.Collections.Generic;

namespace FrontEnd
{
    public class SharedDataSource
    {
        private static SharedDataSource _instance = null;
        private Queue<string> _messageQueue;
        public event EventHandler AddedResponse;
        private Queue<string> _responseQueue;

        /// <summary>
        /// Constructor for the singleton class to allow for the queues to be created.
        /// </summary>
        private SharedDataSource()
        {
            _messageQueue = new Queue<string>();
            _responseQueue = new Queue<string>();
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
                message = _messageQueue.Dequeue();
            }
            
            return message;
        }

        /// <summary>
        /// Fires off an event handler, if one is subscribed, to process a message when it is added.
        /// </summary>
        private void OnAddResponse()
        {
            AddedResponse?.Invoke(this, EventArgs.Empty);
        }

        /// <summary>
        /// Adds a response to the response queue and activates the response that says that this has been added.
        /// </summary>
        /// <param name="response">The response from the server.</param>
        public void AddResponse(string response)
        {
            lock (_responseQueue)
            {
                _responseQueue.Enqueue(response);
            }
            OnAddResponse();
        }

        /// <summary>
        /// Gets a response from the response queue.
        /// </summary>
        /// <returns>A response from the server.</returns>
        public string GetResponse()
        {
            string response;
            lock (_responseQueue)
            {
                response = _responseQueue.Dequeue();
            }

            return response;
        }
    }
}