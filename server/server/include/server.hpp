


#ifndef DZAGAR_SERVER_HPP
#define DZAGAR_SERVER_HPP


#include <msg/instance.hpp>
#include <msg/error.hpp>
#include <msg/visitor.hpp>

#include <networking/flex_waiter.hpp>
#include <networking/socket.hpp>
#include <networking/socket_server.hpp>

#include <string>
#include <vector>

#include <atomic>
#include <condition_variable>
#include <deque>
#include <functional>
#include <memory>
#include <mutex>
#include <thread>
#include <unordered_map>
#include <unordered_set>

#include <boost/bimap.hpp>
#include <boost/bimap/unordered_set_of.hpp>
#include <boost/optional.hpp>



namespace dzagar 
{
    
    
class server final : 
  public se3313::networking::flex_waiter::activity_visitor, 
  public std::enable_shared_from_this<server>,
  public se3313::msg::request::abstract_message_visitor<>  
{

public:

    typedef se3313::networking::port_t port_t;
    
private:
    
    const port_t _serverPort;
    bool _inActivity;
    std::vector<std::shared_ptr<se3313::networking::socket>> _socketList;
    std::shared_ptr<se3313::networking::flex_waiter> _flexinWaiter;
    std::vector<std::string> _clientNames;
    
public:

    inline
    server(const port_t serverPort)
        : _serverPort(serverPort)
    { }

    ~server();

    /*!
     * \brief Start the server.
     */
    void start();

    /*!
     * \brief Stop the server.
     */
    void stop();
    
    

private:
    
    // INSERT YOUR OPERATIONS BELOW
    void addSocketConnection(const std::shared_ptr<se3313::networking::socket>);
    
    void removeSocketConnection(const std::shared_ptr<se3313::networking::socket>);
    
    void onSocketServer(const std::shared_ptr<se3313::networking::socket_server>);
    
    void onSocket(const se3313::networking::flex_waiter::socket_ptr_t);
    
    void onSTDIN(const std::string& line);
    
    return_t visitLogin(const se3313::msg::request::login& req);
    
    return_t visitMessage(const se3313::msg::request::message& req);
    
    return_t error(const std::string& origSender,const se3313::msg::ErrorCode errCode, const std::string& msgStr);

};

} // end namespace

#endif // DZAGAR_SERVER_HPP
