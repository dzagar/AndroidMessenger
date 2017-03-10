
#include "server.hpp"

int main(int /*argc*/, char** /*argv*/)
{
     se3313::networking::port_t serverPort;
     std::cout << "Server: dzagar" << std::endl;
     std::cout << "Enter port number:" << std::endl;
     std::cin >> serverPort;
     std::shared_ptr<dzagar::server> srv = std::make_shared<dzagar::server>(serverPort);
     srv->start();
}
