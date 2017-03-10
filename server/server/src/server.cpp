
#include <iostream>
#include <string>
#include <vector>

#include <atomic>
#include <condition_variable>
#include <deque>
#include <mutex>
#include <thread>
#include <unordered_map>
#include <unordered_set>

#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>

#include <networking/socket.hpp>
#include <networking/socket_server.hpp>

#include <msg/visitor.hpp>
#include <msg/error.hpp>
#include <msg/login.hpp>
#include <msg/json.hpp>

#include "server.hpp"

using namespace dzagar;


namespace msg = se3313::msg;
namespace net = se3313::networking;

namespace pt  = boost::property_tree;

server::~server()
{
    std::cout << "Stopping the server.";
}

void server::start()
{
    std::cout << "Starting server on port: " << _serverPort << std::endl;
    net::socket_server sockServ(_serverPort);
    std::shared_ptr<net::socket_server> sockServPtr = std::make_shared<net::socket_server>(sockServ);

    _flexinWaiter = std::shared_ptr<net::flex_waiter>(new net::flex_waiter(sockServPtr));
    _inActivity = true;
    while(_inActivity){
      _flexinWaiter->wait(this->shared_from_this(), std::chrono::seconds(1)); //random timeout
    }
}

void server::stop()
{
    
}

void server::onSocketServer(const std::shared_ptr<net::socket_server> socksrv){
  std::cout << "Server - onSocketServer Called" << std::endl;
  addSocketConnection(socksrv->accept());
}
    
void server::onSocket(const net::flex_waiter::socket_ptr_t sockPtr){
  std::cout << "Server - onSocket Called" << std::endl;
  std::string readSock;
  int successful = sockPtr->read(&readSock);
  if (successful > 0){
    std::cout<<"Read socket successfully"<<std::endl;
    pt::ptree json = msg::json::from(readSock);
    pt::write_json(std::cout, json, true);

    std::shared_ptr<msg::instance> incomingMessage = visit(json);
    std::cout<< msg::json::to(incomingMessage->toJson(), true) << std::endl;
    for (int i = 0; i < _socketList.size(); i++){
      _socketList[i]->write(msg::json::to(incomingMessage->toJson()));
    }
    return;
  }
  else if (successful == 0){
    removeSocketConnection(sockPtr);
    std::cout<< "Client has disconnected from server." << std::endl;
    return;
  }
  else {	//something bad happened :(
    std::cout<< "Server error" << std::endl;
  }
}
    
void server::onSTDIN(const std::string& line){
  std::cout << "Server onSTDIN Called" << std::endl;
  if(line.compare("exit") == 0){
    for (int i = 0; i < _socketList.size(); i++){
      removeSocketConnection(_socketList[i]);
    }
    _inActivity = false;
  }
}

void server::addSocketConnection(const std::shared_ptr<net::socket> newSock){
  _socketList.push_back(newSock);
  _flexinWaiter->addSocket(newSock);
}

void server::removeSocketConnection(const std::shared_ptr<net::socket> oldSock){
  _socketList.erase(std::remove(_socketList.begin(), _socketList.end(), oldSock), _socketList.end());
  _flexinWaiter->removeSocket(oldSock);
}

server::return_t server::visitLogin(const msg::request::login& req){
  std::cout << "Entered visitor login" << std::endl;
  std::string clientName = req.sender();
  for (int i = 0; i < _clientNames.size(); i++){
    if(_clientNames[i].compare(clientName) == 0){
      return std::make_shared<msg::response::error>(msg::response::error(msg::instance::SERVER_SENDER, msg::ErrorCode::USER_NAME_IN_USE, "You dun goofed. Username is in use. (Server Error)"));
    }
  }
  _clientNames.push_back(clientName);
  return std::make_shared<msg::response::login>(msg::response::login(req.sender()));
}

server::return_t server::visitMessage(const msg::request::message& req) {
  std::cout<< "Entered visitor msg" << std::endl;
  msg::response::message res = msg::response::message(req.sender(), req.content());
  return std::make_shared<msg::response::message>(res);
}

server::return_t server::error(const std::string& origSender,const msg::ErrorCode errCode, const std::string& msgStr){
  return std::make_shared<msg::response::error>(msg::response::error(origSender, errCode, msgStr));
}

